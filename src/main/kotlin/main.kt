import org.apache.ignite.Ignition
import org.apache.ignite.compute.ComputeJobResult
import org.apache.ignite.compute.ComputeJobAdapter
import java.util.ArrayList
import org.apache.ignite.compute.ComputeJob
import org.apache.ignite.compute.ComputeTaskSplitAdapter
import org.apache.ignite.configuration.IgniteConfiguration


class MatrixSortTask : ComputeTaskSplitAdapter<List<List<Long>>, List<List<Long>>>() {
    public override fun split(gridSize: Int, matrix: List<List<Long>>): List<ComputeJob> {
        val jobs: MutableList<ComputeJob> = ArrayList(matrix.size)
        for (row in matrix) {
            jobs.add(object : ComputeJobAdapter() {
                override fun execute(): Any {
                    println(">>> Printing '$row' on from compute job.")

                    return row.toMutableList().apply {
                        add(0, row.sum())
                        toList()
                    }
                }
            })
        }
        return jobs
    }

    override fun reduce(results: List<ComputeJobResult>): List<List<Long>> {
        return results
            .map { it.getData<List<Long>>() }
            .sortedBy { it[0] }
            .map { it.slice(1 until it.size) }
    }

}


fun main(args: Array<String>) {
    val cfg = IgniteConfiguration()
    cfg.isPeerClassLoadingEnabled = true;
    val ignite = Ignition.start(cfg)
    val matrix = listOf(
        listOf<Long>(5, 8, 9, 600),
        listOf<Long>(5, 8, 9, 500),
        listOf<Long>(4, 3, 58, 0))
    ignite.use { it ->
        val sortedMatrix = it.compute().execute(MatrixSortTask(), matrix)
        println("Отсортированная матрица:\n$sortedMatrix")
    }
}